package be.icc.controller;

import be.icc.dto.BidderDto;
import be.icc.dto.ProductDto;
import be.icc.dto.UserDto;
import be.icc.entity.Product;
import be.icc.form.AddProductForm;
import be.icc.form.BidForm;
import be.icc.model.FileModel;
import be.icc.service.BidderService;
import be.icc.service.CategoryService;
import be.icc.service.ProductService;
import be.icc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.apache.commons.lang.StringUtils.*;


/**
 * Created by Scohier Dorian on 18-12-18.
 */
@Controller
@RequestMapping("/product")
public class ProductController {

    @Autowired
    ProductService productService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    UserService userService;
    @Autowired
    BidderService bidderService;

    private static final List<String> contentTypes = Arrays.asList("png", "jpeg", "jpg");

    @RequestMapping("/products")
    public String products(Model model, @RequestParam(required = false) String category) {
        List<ProductDto> products;
        if (isNotBlank(category)) {
            CategoryEnum categoryEnum;
            try {
                categoryEnum = CategoryEnum.valueOf(category);
            } catch (Exception e) {
                return "redirect:";
            }
            products = productService.findByCategoryAndSalable(categoryEnum);
        } else {
            products = productService.findAllSalableProduct();
        }
        if (products.isEmpty()) {
            model.addAttribute("error", "error.products.noProducts");
        } else {
            model.addAttribute("size", (int) Math.ceil(products.size()/5.0));
            model.addAttribute("currentPage", 1 );
        }
        model.addAttribute("products", products);
        return "products";
    }

    @RequestMapping("/newProduct")
    public String newProduct(Model model, @RequestParam(required = false) String error) {
        if ("anonymousUser".equals(SecurityContextHolder.getContext().getAuthentication().getPrincipal())) {
            return "redirect:/connect";
        }
        if (!model.containsAttribute("addProductForm")) {
            model.addAttribute("addProductForm", new AddProductForm());
        }
        initialiseModelForAddAndUpdate(model, error);
        return "addProduct";
    }

    @RequestMapping("/updateProduct")
    public String updateProduct(@ModelAttribute("addProductForm")  AddProductForm updateProductForm,Model model, @RequestParam(required = false) String error) {
        ProductDto productDto = productService.findById(updateProductForm.getId());
        if (!productDto.getSeller().getUsername().equals(((UserDto)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername())) {
            return "redirect:/product/details?id=" + updateProductForm.getId();
        }
        if (!model.containsAttribute("addProductForm") || updateProductForm != null) {
            AddProductForm addProductForm = new AddProductForm();
            addProductForm.setId(productDto.getId());
            addProductForm.setCategory(productDto.getCategory().getCategory());
            addProductForm.setDescription(productDto.getDescription());
            addProductForm.setName(productDto.getName());
            addProductForm.setPrice(productDto.getPrice());
            if (productDto.isAuction()) {
                addProductForm.setAuctionOrFixPrice("auction");
                addProductForm.setPriceAuction(productDto.getPrice());
                String dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm").format(productDto.getEndDate());
                addProductForm.setEndDateString(left(dateFormat, indexOf(dateFormat, " ")));
                addProductForm.setEndTimeString(dateFormat.substring(indexOf(dateFormat, " ") + 1));
            }
            model.addAttribute("addProductForm", addProductForm);
        }
        initialiseModelForAddAndUpdate(model, error);
        return "addProduct";
    }

    private void initialiseModelForAddAndUpdate(Model model, String error) {
        if (!model.containsAttribute("fileModel")) {
            FileModel fileModel = new FileModel();
            model.addAttribute("fileModel", fileModel);
        }
        if (!model.containsAttribute("categoryList")) {
            model.addAttribute("categoryList", CategoryEnum.values());
        }
        if (isNotBlank(error)) {
            switch (error) {
                case "NoPicture":
                    model.addAttribute("error", "error.add.noPicture");
                    break;
                case "PictureFormat":
                    model.addAttribute("error", "error.add.pictureFormat");
                    break;
                case "PictureError":
                    model.addAttribute("error", "error.add.pictureError");
                    break;
            }
        }
    }

    @RequestMapping("/add")
    public String add(@ModelAttribute("addProductForm") @Valid AddProductForm addProductForm, BindingResult result,
                      RedirectAttributes attr, HttpServletRequest request, @RequestParam MultipartFile file) {
        String redirect = checkError(result, attr, addProductForm);
        if (redirect != null) {
            return redirect;
        }

        String filePath = uploadFile(addProductForm, attr, file);

        ProductDto productDto = new ProductDto();
        productDto.setDescription(addProductForm.getDescription().replace("\n", "<br>"));
        productDto.setName(addProductForm.getName());
        if("auction".equals(addProductForm.getAuctionOrFixPrice())) {
            productDto.setAuction(true);
            productDto.setEndDate(addProductForm.getEndDate());
        }
        productDto.setPicture(filePath);
        productDto.setPrice(addProductForm.getPrice());
        productDto.setCategory(categoryService.createOrGetIfExists(addProductForm.getCategory()));
        productDto.setSeller(userService.findByUsername(((UserDto) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername()));
        productDto.setCreationDate(new Date());
        productDto = productService.add(productDto);
        return "redirect:/product/details?id=" + productDto.getId();
    }

    @RequestMapping("/update")
    public String update(@ModelAttribute("addProductForm") @Valid AddProductForm addProductForm, BindingResult result,
                      RedirectAttributes attr, HttpServletRequest request, @RequestParam MultipartFile file) {
        String redirect = checkError(result, attr, addProductForm);
        if (redirect != null) {
            return redirect;
        }
        String filePath = null;
        if (isNotBlank(file.getOriginalFilename())) {
            filePath = uploadFile(addProductForm, attr, file);
        }
        Product product = productService.findEntityById(addProductForm.getId());
        product.setDescription(addProductForm.getDescription().replace("\n", "<br>"));
        product.setName(addProductForm.getName());
        if("auction".equals(addProductForm.getAuctionOrFixPrice())) {
            product.setAuction(true);
            product.setEndDate(addProductForm.getEndDate());
        }
        if(isNotBlank(filePath)) {
            product.setPicture(filePath);
        }
        product.setPrice(addProductForm.getPrice());
        product.setCategory(categoryService.createOrGetIfExists(addProductForm.getCategory()).toEntity());
        ProductDto productDto = productService.update(product);
        return "redirect:/product/details?id=" + productDto.getId();
    }

    private String uploadFile(AddProductForm addProductForm, RedirectAttributes attr, MultipartFile file) {
        String[] location = addProductForm.getFile().getOriginalFilename().split("\\\\");
        String fileName = location[location.length - 1];
        if (!contentTypes.contains(fileName.split("\\.")[1])) {
            attr.addFlashAttribute("addProductForm", addProductForm);
            return "redirect:/product/newProduct?error=PictureFormat";
        }
        String userName = ((UserDto) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        String directoryName = "D:/tmp/img/" + userName + "/";
        File dir = new File(directoryName);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        File serverFile = new File(dir.getAbsolutePath() + File.separator + fileName);

        //write uploaded image to disk
        try {
            try (InputStream is = file.getInputStream(); BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile))) {
                int i;
                while ((i = is.read()) != -1) {
                    stream.write(i);
                }
                stream.flush();
            }
        } catch (IOException e) {
            System.out.println("error : " + e.getMessage());
        }
        return dir.getAbsolutePath() + "\\" + fileName;
    }

    private String checkError(BindingResult result, RedirectAttributes attr, AddProductForm addProductForm) {
        if (result.hasErrors() || (addProductForm.getFile().isEmpty() && addProductForm.getId() == null)) {
            attr.addFlashAttribute("org.springframework.validation.BindingResult.addProductForm", result);
            attr.addFlashAttribute("addProductForm", addProductForm);
            if (addProductForm.getFile().isEmpty()) {
                return "redirect:/product/newProduct?error=NoPicture";
            } else {
                return "redirect:/product/newProduct";
            }
        }
        return null;
    }

    @RequestMapping("/details")
    public String details(Model model, @RequestParam(required = true) Long id, @RequestParam(required = false) String error, @RequestParam(required = false) String success) {
        ProductDto product = productService.findById(id);
        model.addAttribute("product", product);
        String picture = "";
        try {
            String imgName = product.getPicture();
            BufferedImage bImage = ImageIO.read(new File(imgName));//give the path of an image
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bImage, "jpg", baos);
            baos.flush();
            byte[] imageInByteArray = baos.toByteArray();
            baos.close();
            picture = DatatypeConverter.printBase64Binary(imageInByteArray);
        }catch(IOException e){
            System.out.println("Error: "+e);
            model.addAttribute("error", "error.details.pictureError");
        }
        AddProductForm productForm = new AddProductForm();
        productForm.setId(id);
        BidForm bidForm = new BidForm();
        bidForm.setIdProduct(id);
        bidForm.setNewPrice(product.getPrice()+1);
        model.addAttribute("picture", picture);
        model.addAttribute("updateProductForm", productForm);
        model.addAttribute("bidForm", bidForm);
        if (isNotBlank(error)) {
            switch (error) {
                case "InvalidBid":
                    model.addAttribute("error", "error.bid.invalidBid");
                    break;
                case "EndBid":
                    model.addAttribute("error", "error.bid.endBid");
                    break;
            }
        }
        if ((product.getBidders()).isEmpty()) {
            model.addAttribute("lastBidder", null);

        } else{
            model.addAttribute("lastBidder",product.getBidders().toArray()[product.getBidders().size()-1]);
        }
        return "details";
    }


    @RequestMapping(value = "/bid", method = RequestMethod.POST)
    public String bid(@ModelAttribute("bidForm")  BidForm bidForm, Model model) {
        Product product = productService.findEntityById(bidForm.getIdProduct());
        if (product.getPrice() >= bidForm.getNewPrice()) {
            return "redirect:/product/details?id=" + product.getId() + "&error=InvalidBid";
        }
        if (product.getEndDate().before(new Date())) {
            return "redirect:/product/details?id=" + product.getId() + "&error=EndBid";
        }
        product.setPrice(bidForm.getNewPrice());
        BidderDto bidderDto = new BidderDto();
        bidderDto.setUser((UserDto)SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        bidderDto.setPrice(bidForm.getNewPrice());
        bidderDto.setInsertionDate(new Date());
        bidderDto.setProductId(product.getId());
        product.getBidders().add(bidderService.save(bidderDto).toEntity());
        productService.update(product);
        return "redirect:/product/details?id=" + product.getId();
    }

    @ExceptionHandler(Exception.class)
    public String ErreurExample(HttpServletRequest request, Model model, Exception exception) {
        model.addAttribute("exception",exception);
        model.addAttribute("url",request.getRequestURL());

        return "erreur";
    }
}
